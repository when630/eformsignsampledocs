import React from 'react';
import Header from './Header';
import SideBar from './SideBar';

const Layout = ({ children, onCategoryClick }: { children: React.ReactNode, onCategoryClick: (id: number) => void }) => {
  return (
    <div style={{ display: 'flex' }}>
      <SideBar onCategoryClick={onCategoryClick} />
      <div style={{ flex: 1 }}>
        <Header />
        {children}
      </div>
    </div>
  );
};

export default Layout;