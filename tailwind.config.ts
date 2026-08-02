import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        border: "hsl(214 22% 88%)",
        surface: "hsl(0 0% 100%)",
        muted: "hsl(215 20% 96%)",
        ink: "hsl(224 26% 14%)",
      },
      boxShadow: {
        panel: "0 1px 2px rgb(15 23 42 / 0.06)",
      },
    },
  },
  plugins: [],
} satisfies Config;
