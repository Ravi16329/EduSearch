import React from 'react';

export default function EmptyState({ topic }) {
  return (
    <div className="state-panel">
      <h3>No lessons found</h3>
      <p>
        We couldn't find videos or articles for "{topic}". Try a broader topic,
        a different subject, or switch the language.
      </p>
    </div>
  );
}
