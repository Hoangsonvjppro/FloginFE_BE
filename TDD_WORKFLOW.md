# TDD WORKFLOW - Hướng dẫn phát triển theo Test-Driven Development

## 📖 Nguyên tắc TDD

### Chu trình Red-Green-Refactor
```
🔴 RED → 🟢 GREEN → 🔵 REFACTOR → 🔴 RED → ...
```

1. **🔴 RED**: Viết test trước, test sẽ fail (chưa có implementation)
2. **🟢 GREEN**: Viết code tối thiểu để pass test
3. **🔵 REFACTOR**: Cải thiện code (giữ nguyên test passing)

### Quy tắc 3 luật của TDD (Uncle Bob)
1. Không được viết production code cho đến khi có failing test
2. Chỉ viết test đủ để fail (compile error cũng là fail)
3. Chỉ viết production code đủ để pass failing test

---

## 🎯 ROADMAP PHÁT TRIỂN

### PHASE 1: Backend - User Authentication

#### Step 1: User Entity
```java
// 1️⃣ RED: Viết test
@Test
void shouldCreateUserWithValidData() {
    // Given
    User user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPassword("password123");
    
    // Then
    assertNotNull(user);
    assertEquals("testuser", user.getUsername());
    assertEquals("test@example.com", user.getEmail());
}

// 2️⃣ GREEN: Implement User entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    // Getters/Setters
}

// 3️⃣ REFACTOR: Thêm Lombok, validation, etc.
```

#### Step 2: UserRepository
```java
// 1️⃣ RED: Test repository
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldSaveUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        
        // When
        User saved = userRepository.save(user);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
    }
    
    @Test
    void shouldFindUserByUsername() {
        // TODO: Implement
    }
}

// 2️⃣ GREEN: Implement repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### Step 3: AuthService
```java
// 1️⃣ RED: Test service
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void shouldRegisterNewUser() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        User result = authService.register(request);
        
        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        // TODO: Implement
    }
}

// 2️⃣ GREEN: Implement service
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        return userRepository.save(user);
    }
}
```

#### Step 4: AuthController
```java
// 1️⃣ RED: Test controller
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void shouldRegisterUser() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(authService.register(any())).thenReturn(user);
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}

// 2️⃣ GREEN: Implement controller
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

---

### PHASE 2: Backend - Product CRUD

Áp dụng tương tự như Phase 1:

1. ✅ `Product` entity + tests
2. ✅ `ProductRepository` + tests
3. ✅ `ProductService` + tests
4. ✅ `ProductController` + tests

#### Checklist:
- [ ] Test: Create product
- [ ] Test: Get all products
- [ ] Test: Get product by ID
- [ ] Test: Update product
- [ ] Test: Delete product
- [ ] Test: Validation errors
- [ ] Test: Not found errors

---

### PHASE 3: Frontend - React Components

#### Step 1: LoginForm Component
```javascript
// 1️⃣ RED: Test first
import { render, screen, fireEvent } from '@testing-library/react';
import LoginForm from './LoginForm';

describe('LoginForm', () => {
  test('should render login form', () => {
    render(<LoginForm />);
    
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });
  
  test('should call onSubmit with form data', () => {
    const mockOnSubmit = jest.fn();
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password123' }
    });
    
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    
    expect(mockOnSubmit).toHaveBeenCalledWith({
      username: 'testuser',
      password: 'password123'
    });
  });
});

// 2️⃣ GREEN: Implement component
export default function LoginForm({ onSubmit }) {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  
  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        name="username"
        aria-label="username"
        value={formData.username}
        onChange={(e) => setFormData({...formData, username: e.target.value})}
      />
      <input
        type="password"
        name="password"
        aria-label="password"
        value={formData.password}
        onChange={(e) => setFormData({...formData, password: e.target.value})}
      />
      <button type="submit">Login</button>
    </form>
  );
}

// 3️⃣ REFACTOR: Add styling, validation, error handling
```

#### Step 2: ProductList Component
```javascript
// Similar TDD approach for ProductList, ProductForm, etc.
```

---

### PHASE 4: Integration Tests

#### Backend Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldRegisterAndLoginUser() {
        // 1. Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationtest");
        registerRequest.setEmail("integration@test.com");
        registerRequest.setPassword("password123");
        
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            registerRequest,
            Map.class
        );
        
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        
        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationtest");
        loginRequest.setPassword("password123");
        
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }
}
```

#### Frontend Integration Test
```javascript
describe('Product Flow Integration', () => {
  test('should create, edit, and delete product', async () => {
    // Test complete user flow
  });
});
```

---

## 📊 Test Coverage Metrics

### Mục tiêu:
- **Unit Tests**: ≥ 80% line coverage
- **Branch Coverage**: ≥ 75%
- **Integration Tests**: Cover all critical paths

### Kiểm tra coverage:

#### Backend:
```bash
./mvnw test jacoco:report
# Open: target/site/jacoco/index.html
```

#### Frontend:
```bash
npm test -- --coverage
# Open: coverage/lcov-report/index.html
```

---

## ✅ CHECKLIST DEVELOPMENT

### Backend - Auth Feature
- [ ] User Entity
  - [ ] Basic fields (id, username, email, password)
  - [ ] Validation annotations
  - [ ] Entity relationships (if any)
- [ ] UserRepository
  - [ ] Save user
  - [ ] Find by username
  - [ ] Find by email
  - [ ] Check existence
- [ ] AuthService
  - [ ] Register user
  - [ ] Login user
  - [ ] Password encryption
  - [ ] Duplicate username/email check
- [ ] AuthController
  - [ ] POST /api/auth/register
  - [ ] POST /api/auth/login
  - [ ] Request validation
  - [ ] Error handling

### Backend - Product Feature
- [ ] Product Entity
- [ ] ProductRepository
- [ ] ProductService (CRUD)
- [ ] ProductController (REST API)

### Frontend - Components
- [ ] LoginForm
- [ ] RegisterForm
- [ ] ProductList
- [ ] ProductForm
- [ ] ProductItem

### Frontend - Services
- [ ] HTTP Client setup
- [ ] Auth API service
- [ ] Product API service

### Integration
- [ ] Backend integration tests
- [ ] Frontend integration tests
- [ ] E2E tests (Cypress)

---

## 🎓 Best Practices

### 1. Test Naming
```java
// ❌ Bad
@Test void test1() { }

// ✅ Good
@Test void shouldCreateUserWhenValidDataProvided() { }
@Test void shouldThrowExceptionWhenUsernameIsEmpty() { }
```

### 2. AAA Pattern
```java
@Test
void shouldDoSomething() {
    // Arrange (Given)
    // Setup test data
    
    // Act (When)
    // Execute the code under test
    
    // Assert (Then)
    // Verify the results
}
```

### 3. One Assertion Per Test (when possible)
```java
// ❌ Multiple unrelated assertions
@Test
void multipleTests() {
    assertEquals(expected1, actual1);
    assertEquals(expected2, actual2); // Different concept
}

// ✅ Separate tests
@Test void shouldValidateName() { }
@Test void shouldValidateEmail() { }
```

### 4. Test Independence
- Tests không phụ thuộc vào nhau
- Có thể chạy theo bất kỳ thứ tự nào
- Cleanup sau mỗi test

---

## 🚀 Quick Commands

```bash
# Backend
cd backend
./mvnw test                          # Run all tests
./mvnw test -Dtest=AuthServiceTest   # Run specific test
./mvnw clean install                 # Build project
./mvnw spring-boot:run               # Run application

# Frontend
cd frontend
npm test                             # Run tests (watch mode)
npm test -- --coverage               # With coverage
npm test -- --watchAll=false         # Run once
npm start                            # Run dev server
npm run build                        # Build for production

# Coverage reports
./mvnw test jacoco:report            # Backend coverage
npm test -- --coverage               # Frontend coverage
```

---

## 📚 Resources

- [Kent Beck - Test Driven Development](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)
- [Uncle Bob - The Three Laws of TDD](http://butunclebob.com/ArticleS.UncleBob.TheThreeRulesOfTdd)
- [Martin Fowler - Mocks Aren't Stubs](https://martinfowler.com/articles/mocksArentStubs.html)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)

---

**Happy TDD Coding! 🧪✨**
