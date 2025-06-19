# Koco Backend 🚀
![Image](https://media.disquiet.io/images/product/gallery/56ef2b09b5ae81fd4f3e6ff821d394140776f05dd4baecf5134d2b4f3228fcbe?w=1200)

### 🚀 코딩테스트, 더 이상 혼자 고민하지 마세요, 
**당신의 실전 역량을 완성하는 알고리즘 학습 서비스 – Koco**

**[✨ Koco 사용해보기](https://ktbkoco.com/)**  
**[✨ Koco Disquiet 바로가기](https://disquiet.io/prod기ct/koco)**

## 🔒 Rules

### Branch

- 이슈 기반 브랜치 생성
    - `ex) feat/이슈번호-기능명`
- `feat/`
  - 기능 개발 브랜치 → 추후 `dev`으로 머지
- `fix/` 
  - 버그 수정 브랜치 → 추후 `dev`으로 머지
- `refactor/` 
  - 리팩토링 브랜치 → 추후 `dev`으로 머지
- `dev`
  - default branch, 기능 개발 및 버그 수정 후 여기에 머지 and 테스트
- `main`
  - 배포하는 브랜치, 매 배포 시 `dev`의 내용을 가져와서 배포

### Commit
`태그이름`: 작업내용

| 태그       | 설명                                                                 |
|------------|----------------------------------------------------------------------|
| `Remove`     | 파일 삭제                                                             |
| `Chore  `    | 그 이외의 잡일 / 버전 코드 수정, 패키지 구조 변경, 파일 이동, 파일 이름 변경 |
| `Add`        | 코드 변경 없는 단순 파일 추가, 에셋 및 라이브러리 추가               |
| `Fix `       | 버그, 오류 해결, 코드 수정                                           |
| `Style`      | 코드 포맷팅, 코드 변경이 없는 경우, 주석 수정                         |
| `Docs `      | README나 WIKI 등의 문서 개정                                         |
|` Refactor `  | 전면 수정이 있을 때 사용 (코드 구조를 새로 바꾼다거나 등)            |
|` Test   `    | 테스트 모드, 리팩토링 테스트 코드 추가                               |