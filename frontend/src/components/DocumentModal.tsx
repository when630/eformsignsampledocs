import React from 'react';
import './DocumentModal.css';
import { Document } from '../utils/types';

type Props = {
  document: Document;
  onClose: () => void;
};

const DocumentModal = ({ document, onClose }: Props) => {
  const handleDownload = () => {
    const accountStr = localStorage.getItem('account');
    if (!accountStr) {
      alert('로그인이 필요합니다.');
      return;
    }

    const account = JSON.parse(accountStr);
    const accountId = account.id;

    // 다운로드 요청
    window.open(`http://localhost:8080/api/documents/download/${document.id}?accountId=${accountId}`, '_blank');
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <span>{document.title}</span>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          <img
            src={`http://localhost:8080/api/documents/thumbnail/${document.storageId}`}
            alt="썸네일"
            className="modal-thumbnail"
          />
        </div>
        <div className="modal-footer">
          <button onClick={handleDownload}>다운로드</button>
          <button className="primary">템플릿으로 보내기</button>
        </div>
      </div>
    </div>
  );
};

export default DocumentModal;