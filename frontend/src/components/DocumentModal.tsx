// src/components/DocumentModal.tsx

import React from 'react';
import './DocumentModal.css';
import { Document } from '../utils/types';
import { openEformsignTemplate } from '../utils/embedTemplate';

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

    window.open(`http://localhost:8080/api/documents/download/${document.id}?accountId=${accountId}`, '_blank');
  };

  const handleSendToTemplate = async () => {
    const accountStr = localStorage.getItem('account');
    if (!accountStr) {
      alert('로그인이 필요합니다.');
      return;
    }

    const localAccount = JSON.parse(accountStr);
    const accountId = localAccount.id;

    const response = await fetch(`http://localhost:8080/api/documents/base64/${document.storageId}?accountId=${accountId}`);
    const result = await response.json();

    console.log("base64:", result.base64);
    console.log("account:", result.account);
    console.log("company_id:", result.account?.company_id);
    console.log("access_token:", result.token?.access_token);
    console.log("base64 길이:", result.base64?.length);
    console.log("document_id:", result.document?.id);

    if (!result || !result.base64) {
      alert('문서를 불러오지 못했습니다.');
      return;
    }

    openEformsignTemplate({
      account: result.account,
      token: result.token,
      document,
      base64: result.base64,
    });
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
          <button className="primary" onClick={handleSendToTemplate}>템플릿으로 보내기</button>
        </div>
      </div>
    </div>
  );
};

export default DocumentModal;