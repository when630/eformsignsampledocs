// src/components/DocumentModal.tsx
import React, { useEffect, useState } from 'react';
import './DocumentModal.css';
import { Document } from '../utils/types';
import { openEformsignTemplate } from '../utils/embedTemplate';
import type { CopyrightView } from '../utils/types';

type Props = {
  document: Document;
  onClose: () => void;
};

// 모든 필드가 null/빈 값이면 true
const isEmptyCopyright = (c?: CopyrightView | null) => {
  if (!c) return true;
  const {
    copyrightId, type, name, uploaderName, url, published, displayText,
  } = c;
  return (
    copyrightId == null &&
    (type == null) &&
    (!name || name.trim() === '') &&
    (!uploaderName || uploaderName.trim() === '') &&
    (!url || url.trim() === '') &&
    (!published || published.trim() === '') &&
    (!displayText || displayText.trim() === '')
  );
};

const DocumentModal = ({ document, onClose }: Props) => {
  const [copyright, setCopyright] = useState<CopyrightView | null>(null);
  const [loadingCopyright, setLoadingCopyright] = useState(false);
  const [copyrightErr, setCopyrightErr] = useState<string | null>(null);

  useEffect(() => {
    const fetchCopyright = async () => {
      try {
        setLoadingCopyright(true);
        setCopyrightErr(null);
        const res = await fetch(`http://localhost:8080/api/documents/${document.id}/copyright`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        setCopyright(data ?? null);
      } catch (e: any) {
        setCopyrightErr(e.message ?? 'failed');
        setCopyright(null);
      } finally {
        setLoadingCopyright(false);
      }
    };
    fetchCopyright();
  }, [document.id]);

  const handleDownload = () => {
    const accountStr = localStorage.getItem('account');
    if (!accountStr) {
      alert('로그인이 필요합니다.');
      return;
    }
    const accountId = JSON.parse(accountStr).id;
    window.open(
      `http://localhost:8080/api/documents/download/${document.id}?accountId=${accountId}`,
      '_blank'
    );
  };

  const handleSendToTemplate = async () => {
    const accountStr = localStorage.getItem('account');
    if (!accountStr) {
      alert('로그인이 필요합니다.');
      return;
    }
    const accountId = JSON.parse(accountStr).id;

    const response = await fetch(
      `http://localhost:8080/api/documents/base64/${document.storageId}?accountId=${accountId}`
    );
    const result = await response.json();

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

        <div className="copyright" aria-live="polite">
          {loadingCopyright ? (
            <p>출처 정보를 불러오는 중…</p>
          ) : copyrightErr ? (
            <p>출처 정보를 불러오지 못했습니다.</p>
          ) : !isEmptyCopyright(copyright) ? (
            <>
              {copyright?.url ? (
                <a href={copyright.url} target="_blank" rel="noopener noreferrer">
                  {copyright?.name ?? '출처'}
                </a>
              ) : (
                <span>{copyright?.name ?? '출처'}</span>
              )}
              <p>{copyright?.displayText ?? ''}</p>
            </>
          ) : (
            <p>'서식'이란 본 서식을 포함하여, 이폼샘플을 통해 제공되는 모든 서식을 의미합니다.</p>
          )}
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