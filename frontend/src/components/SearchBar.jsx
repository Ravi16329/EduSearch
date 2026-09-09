import React, { useState } from 'react';

const SUBJECTS = ['Math', 'Physics', 'English', 'Custom'];
const LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'hi', label: 'Hindi' },
  { code: 'te', label: 'Telugu' },
];

/**
 * @param {{ onSearch: (params: {topic: string, subject: string, lang: string}) => void, isLoading: boolean, initial?: {topic: string, subject: string, lang: string} }} props
 */
export default function SearchBar({ onSearch, isLoading, initial }) {
  const [subject, setSubject] = useState(initial?.subject ?? 'Math');
  const [topic, setTopic] = useState(initial?.topic ?? '');
  const [lang, setLang] = useState(initial?.lang ?? 'en');

  function handleSubmit(e) {
    e.preventDefault();
    const trimmedTopic = topic.trim();
    if (!trimmedTopic) return;
    onSearch({ topic: trimmedTopic, subject, lang });
  }

  return (
    <form className="search-form" onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="subject">Subject</label>
        <select id="subject" value={subject} onChange={(e) => setSubject(e.target.value)}>
          {SUBJECTS.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="topic">Topic</label>
        <input
          id="topic"
          type="text"
          placeholder="e.g. Laplace Transform"
          value={topic}
          onChange={(e) => setTopic(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label htmlFor="lang">Language</label>
        <select id="lang" value={lang} onChange={(e) => setLang(e.target.value)}>
          {LANGUAGES.map((l) => (
            <option key={l.code} value={l.code}>{l.label}</option>
          ))}
        </select>
      </div>

      <button className="search-submit" type="submit" disabled={isLoading || !topic.trim()}>
        {isLoading ? 'Searching…' : 'Find lessons'}
      </button>
    </form>
  );
}

export { LANGUAGES, SUBJECTS };
