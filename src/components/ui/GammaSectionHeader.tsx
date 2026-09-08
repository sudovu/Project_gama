import React from 'react';

interface GammaSectionHeaderProps {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}

export const GammaSectionHeader: React.FC<GammaSectionHeaderProps> = ({ title, subtitle, action }) => {
  return (
    <div className="flex items-end justify-between mb-4">
      <div>
        <h2 className="text-xl font-display font-bold text-gamma-text-primary">
          {title}
        </h2>
        {subtitle && (
          <p className="text-sm text-gamma-text-secondary mt-0.5">{subtitle}</p>
        )}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
};
