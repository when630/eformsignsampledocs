import React from 'react';
import Header from './Header';
import SideBar from './SideBar';
import './Layout.css';

const Layout = ({ children, onCategoryClick }: { children: React.ReactNode, onCategoryClick: (id: number) => void }) => {
  return (
    <div className='layout-container' style={{ display: 'flex', margin: '5px'}}>
      <SideBar onCategoryClick={onCategoryClick} />
      <div style={{ flex: 1 }}>
        <Header />
        <div className='content-area'>
          {children}
        </div>
      </div>
    </div>
  );
};

export default Layout;