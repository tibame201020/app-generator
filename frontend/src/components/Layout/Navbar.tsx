import React from 'react';

const Navbar: React.FC = () => {
  return (
    <div className="navbar bg-base-100 border-b border-base-300">
      <div className="flex-1">
        <a className="btn btn-ghost text-xl">Jules Software Factory</a>
      </div>
      <div className="flex-none">
        <div className="badge badge-primary badge-outline">Phase 4: Frontend UI</div>
      </div>
    </div>
  );
};

export default Navbar;
