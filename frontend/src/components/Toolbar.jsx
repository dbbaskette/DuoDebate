import React, { useState } from 'react';
import '../styles/Toolbar.css';

const Toolbar = ({ onSubmit, isLoading }) => {
  const [prompt, setPrompt] = useState('');
  const [maxIterations, setMaxIterations] = useState(10);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (prompt.trim() && !isLoading) {
      onSubmit({ prompt: prompt.trim(), maxIterations });
      setPrompt('');
    }
  };

  return (
    <div className="toolbar">
      <form onSubmit={handleSubmit} className="toolbar-form">
        <div className="input-group">
          <textarea
            className="prompt-input"
            placeholder="Enter your debate topic (e.g., 'Outline a blog on AI in technical marketing')..."
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            disabled={isLoading}
            rows={3}
          />
          <div className="controls">
            <div className="iteration-control">
              <label htmlFor="maxIterations">Max Rounds:</label>
              <input
                type="number"
                id="maxIterations"
                min="1"
                max="20"
                value={maxIterations}
                onChange={(e) => setMaxIterations(parseInt(e.target.value))}
                disabled={isLoading}
              />
            </div>
            <button
              type="submit"
              className="submit-button"
              disabled={!prompt.trim() || isLoading}
            >
              {isLoading ? (
                <span className="loading-spinner">⏳</span>
              ) : (
                <span>Start Debate 🚀</span>
              )}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default Toolbar;
