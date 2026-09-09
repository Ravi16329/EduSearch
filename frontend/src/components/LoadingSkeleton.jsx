import React from 'react';

export default function LoadingSkeleton() {
  return (
    <div aria-live="polite" aria-busy="true">
      <div className="skeleton skeleton-hero" />
      <div className="skeleton skeleton-line" />
      <div className="skeleton skeleton-line" />
      <div className="skeleton skeleton-line" />
    </div>
  );
}
