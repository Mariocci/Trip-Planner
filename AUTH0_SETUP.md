# Auth0 Setup Guide

## 1. Create Auth0 Account

1. Go to https://auth0.com
2. Sign up for a free account
3. Create a new tenant (e.g., `trip-planner-dev`)

## 2. Create Application

1. In Auth0 Dashboard, go to **Applications** → **Applications**
2. Click **Create Application**
3. Name: `Trip Planner`
4. Application Type: **Regular Web Application**
5. Click **Create**

## 3. Configure Application Settings

In your application settings, configure:

### Allowed Callback URLs
```
http://localhost:8080/login/oauth2/code/auth0
```

### Allowed Logout URLs
```
http://localhost:5173
```

### Allowed Web Origins
```
http://localhost:5173
```

### Allowed Origins (CORS)
```
http://localhost:5173
```

## 4. Get Credentials

From the application settings page, copy:
- **Domain** (e.g., `dev-abc123.us.auth0.com`)
- **Client ID**
- **Client Secret**

## 5. Configure Local Environment

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and add your Auth0 credentials:
   ```properties
   AUTH0_DOMAIN=your-tenant.us.auth0.com
   AUTH0_CLIENT_ID=your_client_id_here
   AUTH0_CLIENT_SECRET=your_client_secret_here
   JWT_SECRET=generate_a_secure_random_string_here
   ```

3. Generate JWT Secret (at least 256 bits):
   ```bash
   # On Linux/Mac:
   openssl rand -base64 32
   
   # On Windows PowerShell:
   [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
   ```

## 6. Set Environment Variables

### Option A: Using .env file (Recommended for development)

Spring Boot doesn't natively support `.env` files. You can:

1. **Use IntelliJ IDEA EnvFile plugin**
2. **Or manually set environment variables in Run Configuration**
3. **Or use spring-dotenv dependency** (add to pom.xml):
   ```xml
   <dependency>
       <groupId>me.paulschwarz</groupId>
       <artifactId>spring-dotenv</artifactId>
       <version>4.0.0</version>
   </dependency>
   ```

### Option B: Set environment variables manually

**Windows PowerShell:**
```powershell
$env:AUTH0_DOMAIN="your-tenant.us.auth0.com"
$env:AUTH0_CLIENT_ID="your_client_id"
$env:AUTH0_CLIENT_SECRET="your_client_secret"
$env:JWT_SECRET="your_jwt_secret"
```

**Linux/Mac:**
```bash
export AUTH0_DOMAIN="your-tenant.us.auth0.com"
export AUTH0_CLIENT_ID="your_client_id"
export AUTH0_CLIENT_SECRET="your_client_secret"
export JWT_SECRET="your_jwt_secret"
```

## 7. Create Test Users

### Option A: Auth0 Dashboard
1. Go to **User Management** → **Users**
2. Click **Create User**
3. Enter email and password
4. Click **Create**

### Option B: Enable Social Connections
1. Go to **Authentication** → **Social**
2. Enable Google, Facebook, or other providers
3. Configure each provider with their credentials

## 8. Test Authentication Flow

1. Start the backend:
   ```bash
   cd presentation
   mvn spring-boot:run
   ```

2. Start the frontend:
   ```bash
   cd frontend
   npm run dev
   ```

3. Open browser: http://localhost:5173
4. Click "Login" button
5. You should be redirected to Auth0 login page
6. After successful login, you'll be redirected back to the app

## 9. Production Deployment

For production, update:

1. **Allowed Callback URLs**: `https://your-domain.com/login/oauth2/code/auth0`
2. **Allowed Logout URLs**: `https://your-domain.com`
3. **Allowed Web Origins**: `https://your-domain.com`
4. Set environment variables on your hosting platform (Heroku, AWS, Azure, etc.)

## Troubleshooting

### "Invalid redirect_uri"
- Check that callback URL in Auth0 matches exactly: `http://localhost:8080/login/oauth2/code/auth0`
- No trailing slash!

### "Client authentication failed"
- Verify CLIENT_ID and CLIENT_SECRET are correct
- Check that environment variables are loaded

### "CORS error"
- Add `http://localhost:5173` to Allowed Web Origins in Auth0

### "JWT secret too short"
- JWT secret must be at least 256 bits (32 characters in base64)

## Additional Resources

- [Auth0 Spring Boot Quickstart](https://auth0.com/docs/quickstart/webapp/java-spring-boot)
- [Auth0 React Quickstart](https://auth0.com/docs/quickstart/spa/react)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
