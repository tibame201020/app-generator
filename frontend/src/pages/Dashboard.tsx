import React from 'react';

const Dashboard: React.FC = () => {
  return (
    <div className="hero min-h-[50vh] bg-base-200 rounded-box">
      <div className="hero-content text-center">
        <div className="max-w-md">
          <h1 className="text-5xl font-bold">Welcome, Jules</h1>
          <p className="py-6">
            Jules Software Factory is online. <br/>
            Current Phase: <span className="badge badge-primary">Phase 4: Frontend UI</span>
          </p>
          <button className="btn btn-primary">Start New Project</button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
