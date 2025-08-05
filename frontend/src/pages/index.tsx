import React, { useState } from 'react';
import Layout from '../components/Layout';
import { getDocumentsByCategory } from '../services/api';
import { Document } from '../utils/types';

const MainPage = () => {
  const [documents, setDocuments] = useState<Document[]>([]);

  const handleCategoryClick = async (categoryId: number) => {
    try {
      const docs = await getDocumentsByCategory(categoryId);
      setDocuments(docs);
    } catch (error) {
      console.error('문서 불러오기 실패', error);
    }
  };

  return (
    <Layout onCategoryClick={handleCategoryClick}>
      <div style={{ padding: '2rem' }}>
        <h2>문서 목록</h2>
        <ul>
          {documents.map((doc) => (
            <li key={doc.id}>{doc.title}</li>
          ))}
        </ul>
      </div>
    </Layout>
  );
};

export default MainPage;