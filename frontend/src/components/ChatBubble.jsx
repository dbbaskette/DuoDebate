import React from 'react';
import MarkdownRenderer from './MarkdownRenderer';
import '../styles/ChatBubble.css';

const ChatBubble = ({ message }) => {
  const { role, content, model, iteration } = message;
  const isProposer = role === 'PROPOSER';

  return (
    <div className={`chat-bubble-container ${isProposer ? 'proposer' : 'challenger'}`}>
      <div className={`chat-bubble ${isProposer ? 'proposer' : 'challenger'}`}>
        <div className="bubble-header">
          <span className="role-badge">{role}</span>
          <span className="model-badge">{model}</span>
          <span className="iteration-badge">Round {iteration}</span>
        </div>
        <div className="bubble-content">
          <MarkdownRenderer content={content} />
        </div>
      </div>
    </div>
  );
};

export default ChatBubble;
