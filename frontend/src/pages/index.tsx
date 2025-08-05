import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { getDocumentsByCategory } from '../services/api';
import './style.css';
import { Document } from '../utils/types';
import DocumentModal from '../components/DocumentModal';

const MainPage = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
  const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);
  const [loadedMap, setLoadedMap] = useState<{ [id: number]: boolean }>({});

  useEffect(() => {
    if (selectedCategoryId !== null) {
      getDocumentsByCategory(selectedCategoryId)
        .then((docs) => {
          setDocuments(docs);
          // 모든 썸네일 로딩 상태 초기화
          const initialMap: { [id: number]: boolean } = {};
          docs.forEach((doc: Document) => {
            initialMap[doc.id] = false;
          });
          setLoadedMap(initialMap);
        })
        .catch((err) => console.error("문서 로드 실패", err));
    }
  }, [selectedCategoryId]);

  const handleImageLoad = (id: number) => {
    setLoadedMap((prev) => ({ ...prev, [id]: true }));
  };

  return (
    <>
      <Layout onCategoryClick={setSelectedCategoryId}>
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
    </>
  );
};

export default MainPage;