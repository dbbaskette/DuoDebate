import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import '../styles/MarkdownRenderer.css';

const MarkdownRenderer = ({ content }) => {
  return (
    <div className="markdown-content">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          // Customize table rendering with proper styling
          table: ({node, ...props}) => (
            <div className="table-wrapper">
              <table className="markdown-table" {...props} />
            </div>
          ),
          th: ({node, ...props}) => <th className="markdown-th" {...props} />,
          td: ({node, ...props}) => <td className="markdown-td" {...props} />,
          tr: ({node, ...props}) => <tr className="markdown-tr" {...props} />,
          // Preserve newlines for better formatting
          p: ({node, ...props}) => <p className="markdown-p" {...props} />,
          ul: ({node, ...props}) => <ul className="markdown-ul" {...props} />,
          ol: ({node, ...props}) => <ol className="markdown-ol" {...props} />,
          li: ({node, ...props}) => <li className="markdown-li" {...props} />,
          h1: ({node, ...props}) => <h1 className="markdown-h1" {...props} />,
          h2: ({node, ...props}) => <h2 className="markdown-h2" {...props} />,
          h3: ({node, ...props}) => <h3 className="markdown-h3" {...props} />,
          h4: ({node, ...props}) => <h4 className="markdown-h4" {...props} />,
          code: ({node, inline, ...props}) =>
            inline ? (
              <code className="markdown-code-inline" {...props} />
            ) : (
              <code className="markdown-code-block" {...props} />
            ),
          pre: ({node, ...props}) => <pre className="markdown-pre" {...props} />,
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownRenderer;
