docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t louie8821/kakao-theme-maker:v1-test \
  --push \
  .