// Use relative URLs in production (same origin), localhost in dev
const API_BASE_URL = import.meta.env.VITE_API_URL ||
  (window.location.hostname === 'localhost' ? 'http://localhost:8080' : '');

export const conductDebate = async (request) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/debate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error conducting debate:', error);
    throw error;
  }
};

export const conductDebateStream = (request, onEvent, onComplete, onError) => {
  // Using fetch-based SSE approach since EventSource only supports GET
  fetch(`${API_BASE_URL}/api/debate/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
    },
    body: JSON.stringify(request),
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      const readStream = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            onComplete();
            return;
          }

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop(); // Keep incomplete line in buffer

          lines.forEach(line => {
            if (line.startsWith('data:')) {
              try {
                const data = JSON.parse(line.substring(5).trim());
                onEvent(data);
              } catch (e) {
                console.error('Failed to parse SSE data:', e);
              }
            }
          });

          readStream();
        }).catch(onError);
      };

      readStream();
    })
    .catch(onError);
};

export const checkHealth = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/health`);
    return response.ok;
  } catch (error) {
    console.error('Health check failed:', error);
    return false;
  }
};

export const getConfig = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/config`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Failed to fetch config:', error);
    return null;
  }
};
