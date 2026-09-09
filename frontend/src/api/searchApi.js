import axios from 'axios';

const client = axios.create({
  baseURL: '/api',
  timeout: 20000,
});

/**
 * @param {{topic: string, subject: string, lang: string}} params
 * @returns {Promise<import('../types').SearchResponse>}
 */
export async function fetchSearchResults({ topic, subject, lang }) {
  try {
    const { data } = await client.get('/search', { params: { topic, subject, lang } });
    return { data, error: null };
  } catch (err) {
    if (err.response && err.response.data) {
      // Clean JSON error shape from GlobalExceptionHandler: { error, message, source, timestamp }
      return { data: null, error: err.response.data };
    }
    return {
      data: null,
      error: {
        error: 'NETWORK_ERROR',
        message: 'Could not reach the server. Check your connection and try again.',
      },
    };
  }
}
