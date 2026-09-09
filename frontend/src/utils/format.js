export function formatViews(viewCount) {
  if (viewCount == null) return '—';
  if (viewCount >= 1_000_000) return `${(viewCount / 1_000_000).toFixed(1)}M views`;
  if (viewCount >= 1_000) return `${(viewCount / 1_000).toFixed(1)}K views`;
  return `${viewCount} views`;
}

export function formatDate(iso) {
  if (!iso) return 'Unknown date';
  try {
    return new Date(iso).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return 'Unknown date';
  }
}

export function formatScore(score) {
  if (score == null) return '—';
  return score.toFixed(2);
}
