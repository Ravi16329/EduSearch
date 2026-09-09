import React from 'react';
import { formatViews, formatDate, formatScore } from '../utils/format.js';

/**
 * @param {{ videos: import('../types').VideoResult[] }} props
 */
export default function VideoList({ videos }) {
  if (!videos || videos.length === 0) return null;

  return (
    <div className="line-list">
      {videos.map((v, i) => (
        <div className="line-item" key={v.videoId}>
          <span className="line-item__rank">{i + 2}</span>
          <div className="line-item__body">
            <h4>
              <a href={v.videoUrl} target="_blank" rel="noreferrer noopener">
                {v.title}
              </a>
            </h4>
            <p className="line-item__sub">
              {v.channel} · {formatViews(v.viewCount)} · {formatDate(v.publishedAt)}
            </p>
          </div>
          <div className="line-item__score">
            <strong>{formatScore(v.score)}</strong>
            <div>score</div>
          </div>
        </div>
      ))}
    </div>
  );
}
