import React, { useEffect, useState } from 'react';
import './SideBar.css';
import { getCategoryTree } from '../services/api';
import { Category } from '../utils/types';

// ✅ props 타입 추가
type Props = {
  onCategoryClick: (categoryId: number) => void;
};

const SideBar = ({ onCategoryClick }: Props) => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    getCategoryTree()
      .then(setCategories)
      .catch((err) => console.error('카테고리 로드 실패', err));
  }, []);

  const toggleExpand = (id: number) => {
    setExpandedIds(prev => {
      const copy = new Set(prev);
      copy.has(id) ? copy.delete(id) : copy.add(id);
      return copy;
    });
  };

  return (
    <div className="sidebar">
      <div className="title">EformSample</div>
      <div className="account-info">
        <div className="account-name">김이폼 님</div>
        <button className="refresh-btn">토큰 갱신</button>
        <button className="logout-btn">로그아웃</button>
      </div>
      <div className="menu">
        {categories.map((category) => (
          <div key={category.id} className="menu-item">
            <div className="menu-title" onClick={() => toggleExpand(category.id)}>
              {category.name}
            </div>
            {expandedIds.has(category.id) && category.children?.length > 0 && (
              <div className="submenu">
                {category.children.map((sub) => (
                  <div
                    key={sub.id}
                    className="submenu-item"
                    onClick={() => onCategoryClick(sub.id)}
                  >
                    {sub.name}
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