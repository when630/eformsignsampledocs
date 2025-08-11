import React, { useState } from 'react';
import './Header.css';

const Header = () => {
  const [searchKeyword, setSearchKeyword] = useState('');

  const handleSearch = () => {
    console.log('검색어:', searchKeyword);
    // TODO: 문서 검색 API 연동 또는 상위 컴포넌트에 전달
  };

  return (
    <div className="header">
      <div className="search-bar">
        <img src="search-big.png" alt="검색" className="search-icon" />
        <input
          type="text"
          placeholder="서식명 검색"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch}>검색</button>
      </div>
      <div className="underline-short" />
    </div>
  );
};

export default Header;