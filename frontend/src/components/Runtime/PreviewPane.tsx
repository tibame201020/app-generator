import React, { useState } from 'react';
import { ExternalLink, RefreshCw } from 'lucide-react';

interface PreviewPaneProps {
  url: string;
  onClose?: () => void;
}

export const PreviewPane: React.FC<PreviewPaneProps> = ({ url, onClose }) => {
  const [iframeKey, setIframeKey] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const handleRefresh = () => {
    setIsLoading(true);
    setIframeKey(prev => prev + 1);
  };

  const handleOpenNewTab = () => {
    window.open(url, '_blank');
  };

  return (
    <div className="flex flex-col h-full bg-white border-l border-gray-700">
      {/* Header */}
      <div className="h-8 bg-gray-100 border-b border-gray-300 flex items-center justify-between px-2 flex-shrink-0">
        <div className="text-xs text-gray-600 truncate flex-1 mr-2 font-mono" title={url}>
          {url}
        </div>
        <div className="flex items-center space-x-1">
          <button
            onClick={handleRefresh}
            className="p-1 hover:bg-gray-200 rounded text-gray-600 transition-colors"
            title="Refresh Preview"
          >
            <RefreshCw size={14} />
          </button>
          <button
            onClick={handleOpenNewTab}
            className="p-1 hover:bg-gray-200 rounded text-gray-600 transition-colors"
            title="Open in New Tab"
          >
            <ExternalLink size={14} />
          </button>
          {onClose && (
            <button
              onClick={onClose}
              className="p-1 hover:bg-gray-200 rounded text-gray-600 transition-colors"
              title="Close Preview"
            >
              <span className="text-xs">✕</span>
            </button>
          )}
        </div>
      </div>

      {/* Iframe */}
      <div className="flex-1 relative">
        {isLoading && (
          <div className="absolute inset-0 flex items-center justify-center bg-gray-50 z-10">
            <div className="text-gray-400 text-sm">Loading preview...</div>
          </div>
        )}
        <iframe
          key={iframeKey}
          src={url}
          className="w-full h-full border-none"
          title="App Preview"
          onLoad={() => setIsLoading(false)}
        />
      </div>
    </div>
  );
};
