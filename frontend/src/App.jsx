import React, { useState } from 'react';
import SearchBar from './components/SearchBar.jsx';
import ResultsPage from './components/ResultsPage.jsx';

export default function App() {
  const [query, setQuery] = useState(null);

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>EduSearch</h1>
        <p>
          Pick a subject and topic, and EduSearch pulls the best-matching YouTube lesson
          and top web articles side by side — ranked separately, on their own terms.
        </p>
      </header>

      <SearchBar onSearch={setQuery} isLoading={false} initial={query} />

      <ResultsPage query={query} />
    </div>
  );
}
