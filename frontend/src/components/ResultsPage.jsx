import React, { useEffect, useState, useCallback } from 'react';
import { fetchSearchResults } from '../api/searchApi.js';
import BestVideoCard from './BestVideoCard.jsx';
import VideoList from './VideoList.jsx';
import ArticleList from './ArticleList.jsx';
import LoadingSkeleton from './LoadingSkeleton.jsx';
import ErrorState from './ErrorState.jsx';
import EmptyState from './EmptyState.jsx';

/**
 * @param {{ query: {topic: string, subject: string, lang: string} }} props
 */
export default function ResultsPage({ query }) {
  const [status, setStatus] = useState('idle'); // idle | loading | success | error
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const runSearch = useCallback(async () => {
    if (!query?.topic) return;
    setStatus('loading');
    setError(null);
    const { data, error: err } = await fetchSearchResults(query);
    if (err) {
      setError(err);
      setStatus('error');
      return;
    }
    setResult(data);
    setStatus('success');
  }, [query]);

  useEffect(() => {
    runSearch();
  }, [runSearch]);

  if (status === 'idle') return null;
  if (status === 'loading') return <LoadingSkeleton />;
  if (status === 'error') return <ErrorState error={error} onRetry={runSearch} />;

  const videos = result?.videos ?? [];
  const articles = result?.articles ?? [];
  const topPick = videos[0];
  const restOfVideos = videos.slice(1);

  if (videos.length === 0 && articles.length === 0) {
    return <EmptyState topic={query.topic} />;
  }

  return (
    <div>
      {result?.fromCache && (
        <p className="cache-badge">Showing a cached result for this search.</p>
      )}

      {topPick && (
        <div className="section">
          <div className="section-heading">
            <h2>Best video pick</h2>
            <p className="ranking-note">{result.videoRankingNote}</p>
          </div>
          <BestVideoCard video={topPick} />
        </div>
      )}

      {restOfVideos.length > 0 && (
        <div className="section">
          <div className="section-heading">
            <h2>More videos on this topic</h2>
          </div>
          <VideoList videos={restOfVideos} />
        </div>
      )}

      <div className="section">
        <div className="section-heading">
          <h2>Top articles</h2>
          <p className="ranking-note">{result.articleRankingNote}</p>
        </div>
        {articles.length > 0 ? (
          <ArticleList articles={articles} />
        ) : (
          <p className="ranking-note" style={{ textAlign: 'left' }}>No article results for this topic.</p>
        )}
      </div>
    </div>
  );
}
