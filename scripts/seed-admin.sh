#!/bin/bash
set -euo pipefail

API_URL="${1:-https://juanvelasco100.click}"

echo '=== Creando usuario administrador ==='

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/setup/admin" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@btgpactual.com",
    "name": "Administrador BTG",
    "password": "Admin2024!",
    "phone": "+573001234567"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 201 ]; then
  echo 'Admin creado exitosamente!'
  echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
  echo "Error ($HTTP_CODE):"
  echo "$BODY"
fi
