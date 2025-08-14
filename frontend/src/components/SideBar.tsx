// src/components/SideBar.tsx
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategoryTree, refreshToken } from '../services/api';
import type { Category } from '../utils/types';
import LegalConsentModalViewOnly from './LegalConsentModal';
import './SideBar.css';

type Props = {
  onCategoryClick: (categoryId: number) => void;
};

const STORAGE_KEY = 'sidebar.expandedIds';

const SideBar = ({ onCategoryClick }: Props) => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());
  const [accountName, setAccountName] = useState<string>('사용자');
  const [openLegalModal, setOpenLegalModal] = useState(false);
  const [activeId, setActiveId] = useState<number | null>(null);
  const navigate = useNavigate();

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

    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      try {
        const arr: number[] = JSON.parse(raw);
        setExpandedIds(new Set(arr));
      } catch { /* ignore */ }
    }
  }, []);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(Array.from(expandedIds)));
  }, [expandedIds]);

  const depth1Ids = useMemo(() => categories.map((c) => c.id), [categories]);

  const toggleExpand = (id: number) => {
    setExpandedIds((prev) => {
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
      alert('토큰 갱신 완료');
    } catch (err) {
      console.error('토큰 갱신 실패', err);
      alert('토큰 갱신 실패');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('account');
    navigate('/login', { replace: true });
  };

  const onClickDepth1 = (id: number) => {
  const wasExpanded = expandedIds.has(id); // 현재 상태 기억
  toggleExpand(id);

  // 펼칠 때만 활성화/콜백
  if (!wasExpanded) {
    setActiveId(id);
    onCategoryClick(id);
  }
  // 접을 때는 아무 것도 하지 않음 → 활성 선택 유지
};

  const onClickDepth2 = (id: number) => {
    setActiveId(id);
    onCategoryClick(id);
  };

  return (
    <div className="sidebar">
      <img src="logo-w@2x.png" alt="이폼사인" className="eformsign-logo" />
      <div className="title">eformSample</div>

      <div className="account-info">
        <div className="account-name">
          {accountName} 님
          <button className="refresh-btn" onClick={handleRefreshToken} title="토큰 갱신">
            ↺
          </button>
        </div>

        <button
          className="sidebar-legal-btn"
          onClick={() => setOpenLegalModal(true)}
          title="서식 안내"
        >
          서식 안내
        </button>

        <button className="logout-btn" onClick={handleLogout}>
          로그아웃
        </button>
      </div>

      <LegalConsentModalViewOnly
        open={openLegalModal}
        onClose={() => setOpenLegalModal(false)}
      />

      <div className="menu">
        {categories.map((d1) => {
          const isExpanded = expandedIds.has(d1.id);
          const isActive = activeId === d1.id || (activeId != null && depth1Ids.includes(activeId) && activeId === d1.id);

          return (
            <div key={d1.id} className={`menu-item ${isActive ? 'active' : ''}`}>
              <div className="menu-title" onClick={() => onClickDepth1(d1.id)}>
                <span className="folder-icon">{isExpanded ? '▾' : '▸'}</span> {d1.name}
              </div>

              {isExpanded && d1.children?.length ? (
                <div className="submenu">
                  {d1.children.map((d2) => {
                    const isActive2 = activeId === d2.id;
                    return (
                      <div
                        key={d2.id}
                        className={`submenu-item ${isActive2 ? 'active' : ''}`}
                        onClick={() => onClickDepth2(d2.id)}
                      >
                        {d2.name}
                      </div>
                    );
                  })}
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default SideBar;