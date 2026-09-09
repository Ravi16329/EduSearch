import React from 'react';

/**
 * @param {{ articles: import('../types').ArticleResult[] }} props
 */
export default function ArticleList({ articles }) {
  if (!articles || articles.length === 0) return null;

  return (
    <div className="line-list">
      {articles.map((a) => (
        <div className="line-item" key={a.url}>
          <span className="line-item__rank">{a.rank}</span>
          <div className="line-item__body">
            <span className="line-item__source">{a.source}</span>
            <h4>
              <a href={a.url} target="_blank" rel="noreferrer noopener">
                {a.title}
              </a>
            </h4>
            <p className="line-item__snippet">{a.snippet}</p>
          </div>
          <div />
        </div>
      ))}
    </div>
  );
}
