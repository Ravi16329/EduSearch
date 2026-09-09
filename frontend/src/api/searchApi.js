import axios from "axios";

// If deployed (GitHub Pages) → use Render backend.
// If local (npm run dev) → use Vite proxy (/api).
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "/api";

const client = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000,
});

/**
 * @param {{topic: string, subject: string, lang: string}} params
 * @returns {Promise<import('../types').SearchResponse>}
 */
export async function fetchSearchResults({ topic, subject, lang }) {
  try {
    const { data } = await client.get("/search", {
      params: { topic, subject, lang },
    });

    return { data, error: null };
  } catch (err) {
    if (err.response && err.response.data) {
      return {
        data: null,
        error: err.response.data,
      };
    }

    return {
      data: null,
      error: {
        error: "NETWORK_ERROR",
        message: "Could not reach the server. Check your connection and try again.",
      },
    };
  }
}