import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import { searchFormsExact } from '../../services/api';
import DocumentModal from '../../components/DocumentModal';
import type { Document } from '../../utils/types';
import './style.css';

type PageResp<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 0-based page
};

const PAGE_SIZE = 12;

const SearchResultsPage: React.FC = () => {
  const [params, setParams] = useSearchParams();
  const navigate = useNavigate();

  const q = (params.get('q') || '').trim();
  const mode = (params.get('mode') || 'WORD') as 'WORD' | 'EQUAL';
  const page = Number(params.get('page') || 0);

  const [resp, setResp] = useState<PageResp<Document> | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);
  const [loadedMap, setLoadedMap] = useState<Record<number, boolean>>({});

  const hasQuery = q.length > 0;

  useEffect(() => {
    if (!hasQuery) { setResp(null); return; }
    let alive = true;
    setLoading(true);
    searchFormsExact({ q, mode, page, size: PAGE_SIZE })
      .then((r) => {
        if (!alive) return;
        const init: Record<number, boolean> = {};
        (r.content as Document[]).forEach(d => { init[d.id] = false; });
        setLoadedMap(init);
        setResp(r as PageResp<Document>);
      })
      .catch(() =>
        alive && setResp({ content: [], totalElements: 0, totalPages: 0, number: 0 })
      )
      .finally(() => alive && setLoading(false));
    return () => { alive = false; };
  }, [q, mode, page, hasQuery]);

  const setPageParam = (p: number) => setParams({ q, mode, page: String(p) });

  const handleImageLoad = (id: number) => {
    setLoadedMap(prev => ({ ...prev, [id]: true }));
  };

  // Layout의 사이드바 카테고리 클릭을 메인으로 위임
  const handleCategoryClick = (id: number) => {
    // 메인에서 받을 수 있도록 state + sessionStorage 둘 다 전달(둘 중 하나만 써도 됨)
    sessionStorage.setItem('pendingCategoryId', String(id));
    navigate('/', { state: { categoryId: id } });
  };

  return (
    <div className="main-container">
      <Layout onCategoryClick={handleCategoryClick}>

        <div className="search-content-area">
          {!hasQuery && <div className="search-empty">검색어를 입력하세요.</div>}
          {hasQuery && loading && <div className="search-empty">검색 중…</div>}
          {hasQuery && !loading && resp && resp.totalElements === 0 && (
            <div className="search-empty">결과가 없습니다.</div>
          )}

          {hasQuery && !loading && resp && resp.totalElements > 0 && (
            <>
              <div className="search-info">
                검색어 <strong>"{q}"</strong>의 검색 결과입니다.
                <span className="search-count"> (총 {resp.totalElements}개 문서)</span>
              </div>

              <div className="document-grid">
                {resp.content.map((doc) => (
                  <div
                    key={doc.id}
                    className="document-card"
                    onClick={() => setSelectedDocument(doc)}
                  >
                    {!loadedMap[doc.id] && (
                      <div className="thumbnail-loading">썸네일 로딩 중...</div>
                    )}
                    <img
                      src={`http://localhost:8080/api/documents/thumbnail/${doc.storageId}`}
                      alt="썸네일"
                      className="doc-thumbnail"
                      style={{ display: loadedMap[doc.id] ? 'block' : 'none' }}
                      onLoad={() => handleImageLoad(doc.id)}
                      onError={() => handleImageLoad(doc.id)}
                    />
                    <div className="doc-title">{doc.title}</div>
                  </div>
                ))}
              </div>

              {resp.totalPages > 1 && (
                <div className="pagination">
                  <button
                    disabled={resp.number === 0}
                    onClick={() => setPageParam(Math.max(0, resp.number - 1))}
                  >
                    이전
                  </button>
                  <span>{resp.number + 1} / {resp.totalPages}</span>
                  <button
                    disabled={resp.number + 1 >= resp.totalPages}
                    onClick={() => setPageParam(resp.number + 1)}
                  >
                    다음
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </Layout>

      {selectedDocument && (
        <DocumentModal
          document={selectedDocument}
          onClose={() => setSelectedDocument(null)}
        />
      )}
    </div>
  );
};

export default SearchResultsPage;