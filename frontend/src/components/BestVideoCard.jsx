import React from 'react';
import { formatViews, formatDate, formatScore } from '../utils/format.js';

/**
 * @param {{ video: import('../types').VideoResult }} props
 */
export default function BestVideoCard({ video }) {
  if (!video) return null;

  return (
    <div className="best-video-card">
      <div className="best-video-card__player">
        <iframe
          src={`https://www.youtube.com/embed/${video.videoId}`}
          title={video.title}
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowFullScreen
        />
      </div>
      <div className="best-video-card__meta">
        <span className="pick-eyebrow">
          Top pick <span className="score-badge">score {formatScore(video.score)}</span>
        </span>
        <h3>{video.title}</h3>
        <span className="channel">{video.channel}</span>
        <div className="best-video-card__stats">
          <span>{formatViews(video.viewCount)}</span>
          <span>{formatDate(video.publishedAt)}</span>
        </div>
      </div>
    </div>
  );
}
