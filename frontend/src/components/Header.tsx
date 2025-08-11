// src/components/Header.tsx
import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Header.css';
import { searchFormsExact, getDocumentById } from '../services/api';
import DocumentModal from './DocumentModal';
import type { Document as DocType } from '../utils/types';

type LiteDoc = { id: number; title: string };

const Header: React.FC = () => {
  const [q, setQ] = useState('');
  const [loading, setLoading] = useState(false);
  const [candidates, setCandidates] = useState<LiteDoc[]>([]);
  const [openDropdown, setOpenDropdown] = useState(false);

  // 모달
  const [openModal, setOpenModal] = useState(false);
  const [selectedDoc, setSelectedDoc] = useState<DocType | null>(null);

  // 키보드 내비게이션
  const [activeIndex, setActiveIndex] = useState<number>(-1);
  const [wasNavigated, setWasNavigated] = useState(false); // ↑/↓를 한 번이라도 눌렀는지
  const listRef = useRef<HTMLUListElement>(null);

  const navigate = useNavigate();
  const dq = useDebounce(q, 300);
  const inputRef = useRef<HTMLInputElement>(null);

  // 드롭다운 닫기 + 상태 초기화
  const closeDropdown = () => {
    setOpenDropdown(false);
    setActiveIndex(-1);
    setWasNavigated(false);
  };

  useEffect(() => {
    const query = dq.trim();
    if (!query) {
      setCandidates([]);
      closeDropdown();
      return;
    }
    let alive = true;
    (async () => {
      try {
        setLoading(true);
        const page = await searchFormsExact({ q: query, mode: 'WORD', page: 0, size: 8 });
        if (!alive) return;
        const items = page?.content ?? [];
        setCandidates(items);
        setOpenDropdown(true);
        setActiveIndex(-1);      // ★ 초기엔 선택 없음 → Enter는 전체 결과
        setWasNavigated(false);  // ★ 방향키 사용 기록 초기화
      } catch {
        if (!alive) return;
        setCandidates([]);
        closeDropdown();
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [dq]);

  const goToFull = () => {
    const query = q.trim();
    if (!query) return;
    navigate(`/search?q=${encodeURIComponent(query)}&mode=WORD`);
    closeDropdown();
  };

  const openDocModal = async (id: number) => {
    try {
      const full = await getDocumentById(id);
      setSelectedDoc(full as DocType);
      setOpenModal(true);
    } catch {
      alert('문서를 불러오지 못했습니다.');
    } finally {
      closeDropdown();
    }
  };

  // 후보 클릭
  const onPick = (id: number) => openDocModal(id);

  // 키보드 핸들링: ↑/↓ 이동 기록, Enter 동작 분기, Esc 닫기
  const onKeyDown: React.KeyboardEventHandler<HTMLInputElement> = (e) => {
    // 드롭다운이 없거나 후보가 없으면 Enter는 전체 결과
    if (!openDropdown || candidates.length === 0) {
      if (e.key === 'Enter') {
        e.preventDefault();
        goToFull();
      }
      return;
    }

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setWasNavigated(true);
      setActiveIndex((i) => {
        const ni = i < 0 ? 0 : (i < candidates.length - 1 ? i + 1 : 0);
        scrollActiveIntoView(ni);
        return ni;
      });
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setWasNavigated(true);
      setActiveIndex((i) => {
        const ni = i < 0 ? candidates.length - 1 : (i > 0 ? i - 1 : candidates.length - 1);
        scrollActiveIntoView(ni);
        return ni;
      });
    } else if (e.key === 'Enter') {
      e.preventDefault();
      // ★ 규칙: 방향키 사용 전 → 전체 결과 / 사용 후 → 현재 항목 모달
      if (wasNavigated && activeIndex >= 0 && activeIndex < candidates.length) {
        openDocModal(candidates[activeIndex].id);
      } else {
        goToFull();
      }
    } else if (e.key === 'Escape') {
      closeDropdown();
    }
  };

  // 선택 항목이 보이도록 스크롤
  const scrollActiveIntoView = (index: number) => {
    const ul = listRef.current;
    if (!ul) return;
    const li = ul.children.item(index) as HTMLElement | null;
    if (li) li.scrollIntoView({ block: 'nearest' });
  };

  return (
    <>
      <div className="header">
        <div
          className="search-bar"
          onFocus={() => candidates.length > 0 && setOpenDropdown(true)}
          onBlur={() => setTimeout(() => setOpenDropdown(false), 150)}
        >
          <img src="search-big.png" alt="검색" className="search-icon" />
          <input
            ref={inputRef}
            type="text"
            placeholder="검색어 입력"
            value={q}
            onChange={(e) => { setQ(e.target.value); }}
            onKeyDown={onKeyDown}
          />
          <button onClick={goToFull}>검색</button>

          {openDropdown && (
            <div className="search-dropdown" role="listbox" aria-activedescendant={activeIndex >= 0 ? `cand-${activeIndex}` : undefined}>
              {loading && <div className="search-dropdown-empty">검색 중…</div>}
              {!loading && candidates.length === 0 && (
                <div className="search-dropdown-empty">결과 없음</div>
              )}
              {!loading && candidates.length > 0 && (
                <>
                  <ul className="search-dropdown-list" ref={listRef}>
                    {candidates.map((doc, i) => (
                      <li
                        id={`cand-${i}`}
                        key={doc.id}
                        className={`search-dropdown-item ${i === activeIndex ? 'active' : ''}`}
                        title={doc.title}
                        onMouseDown={() => onPick(doc.id)}
                        role="option"
                        aria-selected={i === activeIndex}
                      >
                        {doc.title}
                      </li>
                    ))}
                  </ul>
                </>
              )}
            </div>
          )}
        </div>
        <div className="underline-short" />
      </div>

      {openModal && selectedDoc && (
        <DocumentModal
          document={selectedDoc}
          onClose={() => setOpenModal(false)}
        />
      )}
    </>
  );
};

export default Header;

// 디바운스 훅
function useDebounce<T>(value: T, delay = 300) {
  const [v, setV] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setV(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return v;
}