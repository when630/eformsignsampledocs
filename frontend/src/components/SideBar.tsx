import React, { useEffect, useState } from 'react';
import './SideBar.css';
import { getCategoryTree, refreshToken } from '../services/api';
import { Category } from '../utils/types';
import LegalConsentModalViewOnly from './LegalConsentModal';

type Props = {
  onCategoryClick: (categoryId: number) => void;
};

const SideBar = ({ onCategoryClick }: Props) => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());
  const [accountName, setAccountName] = useState<string>('사용자');
  const [openLegalModal, setOpenLegalModal] = React.useState(false);

  useEffect(() => {
    getCategoryTree()
      .then(setCategories)
      .catch((err) => console.error('카테고리 로드 실패', err));

    const stored = localStorage.getItem('account');
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        setAccountName(parsed.name || '사용자');
      } catch {
        setAccountName('사용자');
      }
    }
  }, []);

  const toggleExpand = (id: number) => {
    setExpandedIds(prev => {
      const copy = new Set(prev);
      copy.has(id) ? copy.delete(id) : copy.add(id);
      return copy;
    });
  };

  const handleRefreshToken = async () => {
  try {
    const data = await refreshToken();
    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);
    console.log("프론트 refresh_token:", localStorage.getItem("refresh_token"));
    alert("토큰 갱신 완료");
  } catch (err) {
    console.error("토큰 갱신 실패", err);
    alert("토큰 갱신 실패");
  }
};

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('account');
    window.location.href = '/login';
  };

  return (
    <div className="sidebar">
      <img src="logo-w@2x.png" alt="이폼사인" className="eformsign-logo" />
      <div className="title">eformSample</div>
      <div className="account-info">
        <div className="account-name">{accountName} 님</div>
        <button className="refresh-btn" onClick={handleRefreshToken}>토큰 갱신</button>
        <button
          className="sidebar-legal-btn"
          onClick={() => setOpenLegalModal(true)}
          title="샘플 양식 면책 고지"
        >
          면책 고지
        </button>
        <button className="logout-btn" onClick={handleLogout}>로그아웃</button>
        
      </div>
      <LegalConsentModalViewOnly
        open={openLegalModal}
        onClose={() => setOpenLegalModal(false)}
      />
      <div className="menu">
        {categories.map((depth1) => (
          <div key={depth1.id} className="menu-item">
            <div className="menu-title" onClick={() => toggleExpand(depth1.id)}>
              {depth1.name}
            </div>
            {expandedIds.has(depth1.id) && depth1.children?.length > 0 && (
              <div className="submenu">
                {depth1.children.map((depth2) => (
                  <div
                    key={depth2.id}
                    className="submenu-item"
                    onClick={() => onCategoryClick?.(depth2.id)}
                  >
                    {depth2.name}
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default SideBar;