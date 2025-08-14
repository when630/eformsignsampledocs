import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { getDocumentsByCategory, getCategoryPath } from '../services/api';
import './style.css';
import { Document } from '../utils/types';
import DocumentModal from '../components/DocumentModal';

const MainPage = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
  const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);
  const [loadedMap, setLoadedMap] = useState<Record<number, boolean>>({});
  const [categoryPath, setCategoryPath] = useState<string>('');

  const location = useLocation() as { state?: { categoryId?: number } };
  const navigate = useNavigate();

  const appliedFromRouteRef = useRef(false);

  useEffect(() => {
    if (appliedFromRouteRef.current) return;

    const stateId = location.state?.categoryId;
    const storedId = sessionStorage.getItem('pendingCategoryId');

    if (stateId != null) {
      setSelectedCategoryId(Number(stateId));
      appliedFromRouteRef.current = true;
      navigate('.', { replace: true, state: {} });
      return;
    }

    if (storedId) {
      setSelectedCategoryId(Number(storedId));
      appliedFromRouteRef.current = true;
      sessionStorage.removeItem('pendingCategoryId');
    }
  }, []);

  useEffect(() => {
    if (selectedCategoryId === null) return;

    let alive = true;
    Promise.all([
      getDocumentsByCategory(selectedCategoryId),
      getCategoryPath(selectedCategoryId),
    ])
      .then(([docs, path]) => {
        if (!alive) return;
        setDocuments(docs);

        const initialMap: Record<number, boolean> = {};
        docs.forEach((doc: Document) => { initialMap[doc.id] = false; });
        setLoadedMap(initialMap);

        setCategoryPath(`${path.join(' > ')} (총 ${docs.length}개 문서)`);
      })
      .catch((err) => console.error('문서 또는 카테고리 경로 로드 실패', err));

    return () => { alive = false; };
  }, [selectedCategoryId]);

  const handleImageLoad = (id: number) => {
    setLoadedMap((prev) => ({ ...prev, [id]: true }));
  };

  return (
    <div className='main-container'>
      <Layout onCategoryClick={setSelectedCategoryId}>
        <div className="category-info">
          {categoryPath && <p>{categoryPath}</p>}
        </div>

        <div className="document-grid">
          {documents.map((doc) => (
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

export default MainPage;