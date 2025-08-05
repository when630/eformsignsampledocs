import React, { useEffect, useState } from 'react';
import './SideBar.css';
import { getCategoryTree } from '../services/api';
import { Category } from '../utils/types';

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
      <img src='logo-w@2x.png'
            alt="이폼사인"
            className="eformsign-logo"/>
      <div className="title">eformSample</div>
      <div className="account-info">
        <div className="account-name">김이폼 님</div>
        <button className="refresh-btn">토큰 갱신</button>
        <button className="logout-btn">로그아웃</button>
      </div>
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