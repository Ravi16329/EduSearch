import React from 'react';

/**
 * @param {{ error: {error?: string, message?: string, source?: string}, onRetry: () => void }} props
 */
export default function ErrorState({ error, onRetry }) {
  const isQuota = error?.error === 'API_QUOTA_EXCEEDED';

  return (
    <div className="state-panel state-panel--error" role="alert">
      <h3>{isQuota ? 'Search limit reached' : 'Something went wrong'}</h3>
      <p>{error?.message || 'We could not complete this search. Please try again.'}</p>
      <button className="search-submit" style={{ marginTop: 16 }} onClick={onRetry}>
        Try again
      </button>
    </div>
  );
}
