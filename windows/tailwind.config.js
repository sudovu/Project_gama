/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./src/renderer/index.html",
    "./src/renderer/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        gammaBg: '#0a0a0f',
        gammaSurface: '#12121a',
        gammaSurfaceHover: '#1c1c28',
        gammaCyan: '#00ffcc',
        gammaViolet: '#a855f7',
        gammaPink: '#ec4899',
      },
    },
  },
  plugins: [],
};
