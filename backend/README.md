# Extension Blocker Backend

## 업로드 테스트 API

실제 파일 바이너리는 저장하지 않고, multipart 파일명에서 확장자만 추출해 차단 여부를 검증합니다.
허용된 요청은 파일명과 확장자만 `uploaded_file` 테이블에 저장합니다.

### 요청

```http
POST /api/files/upload
Content-Type: multipart/form-data
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| file | MultipartFile | 검증할 파일 |

### 차단 기준

- `FIXED` 타입이면서 `checked = true`인 확장자
- `CUSTOM` 타입으로 저장된 모든 확장자

파일명 마지막 점 뒤의 값을 확장자로 사용합니다. `archive.tar.gz`는 `gz` 기준으로 검사하며, 추출 후 앞뒤 공백 제거, 소문자 변환, 맨 앞 점 제거 규칙을 적용합니다.

### 고정 확장자 관리 구조

고정 확장자는 별도 컬럼이나 코드의 7개 슬롯으로 관리하지 않고, `extensions` 테이블의 `type = FIXED` row로 관리합니다.
초기 기본값은 `app.fixed-extensions.defaults` 설정으로 seed 되며, 이후 고정 확장자 후보를 늘리거나 줄일 때는 테이블 row를 추가/삭제하면 됩니다.

### 파일 테이블

`uploaded_file`에는 실제 파일 내용이 아니라 파일명과 확장자만 저장합니다.

| 컬럼 | 설명 |
| --- | --- |
| id | 식별자 |
| filename | 파일명 |
| extension | 정규화 확장자 |
| createdAt | 생성 시각 |

추후 확장자 차단 시 기존 파일 존재 여부는 `uploaded_file.extension` 기준으로 조회할 수 있습니다.

### 응답 예시

```json
{
  "allowed": false,
  "extension": "exe",
  "originalFilename": "test.exe",
  "message": "차단된 확장자입니다."
}
```

### curl 예시

차단 확장자 테스트:

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.exe"
```

허용 확장자 테스트:

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.png"
```
