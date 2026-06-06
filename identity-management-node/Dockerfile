# Node.js identity-management service
FROM node:22-alpine

WORKDIR /app

# Install dependencies first (better layer caching).
COPY package.json package-lock.json* ./
RUN npm install --omit=dev

# App source.
COPY src ./src

EXPOSE 5000
CMD ["node", "src/server.js"]
