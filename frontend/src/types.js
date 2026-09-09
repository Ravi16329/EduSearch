/**
 * @typedef {Object} VideoResult
 * @property {string} videoId
 * @property {string} title
 * @property {string} channel
 * @property {string} thumbnailUrl
 * @property {number} viewCount
 * @property {string} publishedAt
 * @property {string} videoUrl
 * @property {number} score
 */

/**
 * @typedef {Object} ArticleResult
 * @property {string} title
 * @property {string} source
 * @property {string} snippet
 * @property {string} url
 * @property {number} rank
 */

/**
 * @typedef {Object} SearchResponse
 * @property {string} topic
 * @property {string} subject
 * @property {string} lang
 * @property {VideoResult[]} videos
 * @property {ArticleResult[]} articles
 * @property {string} videoRankingNote
 * @property {string} articleRankingNote
 * @property {boolean} fromCache
 */

export {};
