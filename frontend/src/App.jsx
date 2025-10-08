import React, { useState, useEffect, useRef } from 'react';
import ChatBubble from './components/ChatBubble';
import GlassCard from './components/GlassCard';
import Toolbar from './components/Toolbar';
import MarkdownRenderer from './components/MarkdownRenderer';
import { conductDebateStream, checkHealth, getConfig } from './api/debateApi';
import './App.css';

function App() {
  const [transcript, setTranscript] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPrompt, setCurrentPrompt] = useState('');
  const [finalDraft, setFinalDraft] = useState('');
  const [sources, setSources] = useState([]);
  const [debateStatus, setDebateStatus] = useState('');
  const [isHealthy, setIsHealthy] = useState(true);
  const [modelConfig, setModelConfig] = useState(null);
  const transcriptEndRef = useRef(null);

  useEffect(() => {
    // Check API health and fetch config on mount
    checkHealth().then(setIsHealthy);
    getConfig().then(setModelConfig);
  }, []);

  useEffect(() => {
    // Auto-scroll to bottom when new messages arrive
    transcriptEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [transcript]);

  const handleDebateSubmit = (request) => {
    setIsLoading(true);
    setTranscript([]);
    setFinalDraft('');
    setSources([]);
    setDebateStatus('');
    setCurrentPrompt(request.prompt);

    conductDebateStream(
      request,
      // onEvent - handle each SSE event
      (event) => {
        console.log('Received event:', event.type);

        switch (event.type) {
          case 'DEBATE_START':
            console.log('Debate started');
            break;

          case 'ITERATION_START':
            console.log('New iteration starting');
            break;

          case 'PROPOSER_RESPONSE':
            if (event.message) {
              setTranscript(prev => [...prev, event.message]);
            }
            break;

          case 'CHALLENGER_RESPONSE':
            if (event.message) {
              setTranscript(prev => [...prev, event.message]);
            }
            break;

          case 'DEBATE_COMPLETE':
            if (event.finalResponse) {
              setFinalDraft(event.finalResponse.finalDraft);
              setSources(event.finalResponse.sources || []);
              setDebateStatus(event.finalResponse.finalStatus);
            }
            setIsLoading(false);
            break;

          case 'ERROR':
            console.error('Debate error:', event.error);
            if (event.message) {
              setTranscript(prev => [...prev, event.message]);
            }
            alert(`Error: ${event.error}`);
            setIsLoading(false);
            break;

          default:
            console.log('Unknown event type:', event.type);
        }
      },
      // onComplete
      () => {
        console.log('Stream completed');
        setIsLoading(false);
      },
      // onError
      (error) => {
        console.error('Stream error:', error);
        setIsLoading(false);
        alert('Failed to conduct debate. Please check if the backend is running.');
      }
    );
  };

  return (
    <div className="app">
      <div className="app-container">
        <header className="app-header">
          <h1 className="app-title">
            <span className="title-duo">Duo</span>
            <span className="title-debate">Debate</span>
          </h1>
          <p className="app-subtitle">
            Watch AI models debate and refine ideas in real-time
          </p>
          {!isHealthy && (
            <div className="health-warning">
              ⚠️ Backend API might be offline
            </div>
          )}
        </header>

        <main className="app-main">
          {transcript.length === 0 && !isLoading && (
            <div className="welcome-screen">
              <GlassCard>
                <div className="welcome-content">
                  <h2>Welcome to DuoDebate</h2>
                  <p>Enter a prompt below to start a debate between two AI models:</p>
                  <ul>
                    <li>
                      <strong>PROPOSER:</strong> Creates and refines drafts
                      {modelConfig?.proposerModel && <div className="model-name">{modelConfig.proposerModel}</div>}
                    </li>
                    <li>
                      <strong>CHALLENGER:</strong> Provides constructive criticism
                      {modelConfig?.challengerModel && <div className="model-name">{modelConfig.challengerModel}</div>}
                    </li>
                  </ul>
                  <p className="welcome-hint">
                    Try: "Outline a blog post on AI in technical marketing"
                  </p>
                </div>
              </GlassCard>
            </div>
          )}

          {currentPrompt && (
            <GlassCard className="prompt-card">
              <div className="prompt-display">
                <strong>Debate Topic:</strong> {currentPrompt}
              </div>
            </GlassCard>
          )}

          <div className="transcript-container">
            {transcript.map((message, index) => (
              <ChatBubble key={index} message={message} />
            ))}
            {isLoading && transcript.length > 0 && (
              <div className="loading-indicator">
                <div className="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            )}
            <div ref={transcriptEndRef} />
          </div>

          {finalDraft && (
            <GlassCard className="final-draft-card">
              <div className="final-draft">
                <div className="final-draft-header">
                  <h3>Final Draft</h3>
                  <div className="final-draft-actions">
                    <button
                      className="action-button copy-button"
                      onClick={() => {
                        navigator.clipboard.writeText(finalDraft);
                        // Show temporary success feedback
                        const btn = document.querySelector('.copy-button');
                        btn.textContent = '✓ Copied!';
                        setTimeout(() => btn.textContent = '📋 Copy', 2000);
                      }}
                      title="Copy to clipboard"
                    >
                      📋 Copy
                    </button>
                    <button
                      className="action-button download-button"
                      onClick={() => {
                        const blob = new Blob([finalDraft], { type: 'text/markdown' });
                        const url = URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;
                        a.download = `debate-${Date.now()}.md`;
                        document.body.appendChild(a);
                        a.click();
                        document.body.removeChild(a);
                        URL.revokeObjectURL(url);
                      }}
                      title="Download as markdown"
                    >
                      ⬇️ Download
                    </button>
                    {sources && sources.length > 0 && (
                      <button
                        className="action-button sources-button"
                        onClick={() => {
                          const sourcesList = sources.map((src, idx) => `${idx + 1}. ${src}`).join('\n');
                          const content = `# Sources\n\n${sourcesList}`;
                          const blob = new Blob([content], { type: 'text/markdown' });
                          const url = URL.createObjectURL(blob);
                          const a = document.createElement('a');
                          a.href = url;
                          a.download = `debate-sources-${Date.now()}.md`;
                          document.body.appendChild(a);
                          a.click();
                          document.body.removeChild(a);
                          URL.revokeObjectURL(url);
                        }}
                        title="Download sources"
                      >
                        📚 Sources ({sources.length})
                      </button>
                    )}
                    <span className={`status-badge ${debateStatus.toLowerCase()}`}>
                      {debateStatus}
                    </span>
                  </div>
                </div>
                <div className="final-draft-content">
                  <MarkdownRenderer content={finalDraft} />
                </div>
              </div>
            </GlassCard>
          )}
        </main>

        <Toolbar onSubmit={handleDebateSubmit} isLoading={isLoading} />
      </div>
    </div>
  );
}

export default App;
