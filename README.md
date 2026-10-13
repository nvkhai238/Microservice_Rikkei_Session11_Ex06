# Session 11 - Bai 6: Xuat kho lo hang lon

Chay `docker compose up -d --wait` va `./gradlew :warehouse-service:bootRun`.

`GET /api/medicines/1` nap cache. `POST /api/shipments` voi JSON `{"medicineId":1,"quantity":1}` lay lock, tru ton kho trong PostgreSQL, commit, xoa cache `medicines::1`, sau do publish JSON len `shipping-alerts`. Subscriber in yeu cau chuan bi xe. Thuoc mau co 100 hop.

Kiem tra tranh ban am kho voi 120 request:

```bash
seq 1 120 | xargs -P 20 -I{} curl -s -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8090/api/shipments -H 'Content-Type: application/json' -d '{"medicineId":1,"quantity":1}' | sort | uniq -c
```

Doi chieu `GET /api/medicines/1` va SQL `SELECT stock FROM medicines WHERE id=1`. Lan chay tiep theo co the bat dau tu ton kho da truoc do.
