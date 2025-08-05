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
      <h1 className="site-title">서식 검색</h1>
      <div className="search-bar">
        <input
          type="text"
          placeholder="서식 검색"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch}>검색</button>
      </div>
    </div>
  );
};

export default Header;