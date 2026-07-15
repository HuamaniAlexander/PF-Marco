document.addEventListener('DOMContentLoaded', () => {
  const toggleButton = document.getElementById('chatbotToggle');
  const closeButton = document.getElementById('chatbotClose');
  const panel = document.getElementById('chatbotPanel');
  const form = document.getElementById('chatbotForm');
  const input = document.getElementById('chatbotInput');
  const messages = document.getElementById('chatbotMessages');
  const storageKey = 'lenabot_conversation';
  const initialMessage = '¡Hola! Soy LeñaBot. Puedo ayudarte con carta, promociones, reservas, delivery, pagos o estado de pedido.';

  if (!toggleButton || !closeButton || !panel || !form || !input || !messages) {
    return;
  }

  let conversation = [];

  const scrollToBottom = () => {
    messages.scrollTop = messages.scrollHeight;
  };

  const escapeHtml = (text) => text
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

  const formatMessage = (text) => escapeHtml(text)
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>');

  const saveConversation = () => {
    localStorage.setItem(storageKey, JSON.stringify(conversation.slice(-20)));
  };

  const addMessage = (text, type = 'bot', persist = true) => {
    const bubble = document.createElement('div');
    bubble.className = `chatbot-message ${type}`;
    bubble.innerHTML = formatMessage(text);
    messages.appendChild(bubble);

    if (persist && !type.includes('loading')) {
      conversation.push({ text, type: type.includes('user') ? 'user' : 'bot' });
      saveConversation();
    }

    scrollToBottom();
    return bubble;
  };

  const getQuickActions = (text) => {
    const normalized = text.toLowerCase();
    const actions = [];

    if (normalized.includes('combo') || normalized.includes('promoci')) {
      actions.push({ label: 'Ver combos', href: '/promociones#combos', icon: 'bi-tags-fill' });
    }

    if (normalized.includes('carta') || normalized.includes('pollo') || normalized.includes('parrilla')) {
      actions.push({ label: 'Ir a la carta', href: '/carta#carta-productos', icon: 'bi-journal-text' });
    }

    if (normalized.includes('reserva') || normalized.includes('mesa')) {
      actions.push({ label: 'Reservar mesa', href: '/reserva', icon: 'bi-calendar-check' });
    }

    if (normalized.includes('pago') || normalized.includes('pedido') || normalized.includes('delivery')) {
      actions.push({ label: 'Armar pedido', href: '/carta#carta-productos', icon: 'bi-bag-check' });
    }

    return actions.filter((action, index, self) =>
      self.findIndex((item) => item.href === action.href) === index
    ).slice(0, 3);
  };

  const addQuickActions = (text) => {
    const actions = getQuickActions(text);
    if (!actions.length) {
      return;
    }

    const wrapper = document.createElement('div');
    wrapper.className = 'chatbot-actions';

    actions.forEach((action) => {
      const link = document.createElement('a');
      link.className = 'chatbot-action';
      link.href = action.href;
      link.innerHTML = `<i class="bi ${action.icon}"></i><span>${action.label}</span>`;
      wrapper.appendChild(link);
    });

    messages.appendChild(wrapper);
    scrollToBottom();
  };

  const restoreConversation = () => {
    const savedConversation = localStorage.getItem(storageKey);

    try {
      conversation = savedConversation ? JSON.parse(savedConversation) : [];
    } catch (error) {
      conversation = [];
    }

    messages.innerHTML = '';

    if (!conversation.length) {
      addMessage(initialMessage, 'bot', true);
      return;
    }

    conversation.forEach((item) => {
      addMessage(item.text, item.type, false);
    });

    const lastBotMessage = [...conversation].reverse().find((item) => item.type === 'bot');
    if (lastBotMessage) {
      addQuickActions(lastBotMessage.text);
    }
  };

  const setPanelVisible = (visible) => {
    panel.hidden = !visible;
    if (visible) {
      input.focus();
      scrollToBottom();
    }
  };

  restoreConversation();

  toggleButton.addEventListener('click', () => {
    setPanelVisible(panel.hidden);
  });

  closeButton.addEventListener('click', () => {
    setPanelVisible(false);
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const message = input.value.trim();
    if (!message) {
      return;
    }

    addMessage(message, 'user');
    input.value = '';
    input.disabled = true;

    const loadingBubble = addMessage('Escribiendo...', 'bot loading', false);

    try {
      const response = await fetch('/chatbot/mensaje', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ mensaje: message })
      });

      const data = await response.json();
      loadingBubble.remove();

      const botResponse = data.respuesta || 'No pude responder en este momento. Intenta nuevamente.';
      addMessage(botResponse, 'bot');
      addQuickActions(`${message} ${botResponse}`);
    } catch (error) {
      loadingBubble.remove();

      const fallback = 'Ups, no pude conectar con el asistente. Escríbenos por WhatsApp y te ayudamos al toque.';
      addMessage(fallback, 'bot');
      addQuickActions(`${message} ${fallback}`);
    } finally {
      input.disabled = false;
      input.focus();
    }
  });
});
