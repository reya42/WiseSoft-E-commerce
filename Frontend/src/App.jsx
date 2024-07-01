import { Navbar } from "./components";
import {BrowserRouter as Router,Routes,Route} from "react-router-dom";

import {Home, Search} from "./pages/";
import {Categories} from "./pages/";
import Brands from "./pages/Brands";
import BrandDetail from "./pages/BrandDetail";
import Products from "./pages/Products";
import SingleProduct from "./pages/SingleProduct";
import Cart from "./pages/Cart";
import Login from "./pages/Login";
import User from "./pages/User";
import Questions from "./pages/Questions";
import Admin from "./pages/AdminPages/Admin";
import AdminProducts from "./pages/AdminPages/AdminProducts";
import ProductForm from "./pages/AdminPages/ProductForm";
import AdminQuestions from "./pages/AdminPages/AdminQuestions";
import QuestionForm from "./pages/AdminPages/QuestionForm";
import Comments from "./pages/Comments";
import Purchases from "./pages/Purchases";

const App = () => {
  return (
      <Router>
          <Navbar/>
        <Routes>
          <Route path="" element={<Home />} />
          <Route path="/categories/:categoryLink" element={<Categories />} />
          <Route path="/search" element={<Search />} />
          <Route path="/brands/:brandLink" element={<BrandDetail />} />
          <Route path="/brands" element={<Brands />} />
          <Route path="/products" element={<Products />} />
          <Route path="/products/:productLink" element={<SingleProduct />} />
          <Route path="/products/:productLink/questions" element={<Questions />} />
          <Route path="/products/:productLink/comments" element={<Comments />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/login" element={<Login admin={false} login={true} />} />
          <Route path="/signup" element={<Login admin={false} login={false} />} />
          <Route path="/admin/login" element={<Login admin={true} login={true} />} />
          <Route path="/user" element={<User />} />
          <Route path="/admin" element={<Admin />} />
          <Route path="/admin/products" element={<AdminProducts />} />
          <Route path="/admin/products/edit/:currentProductLink" element={<ProductForm isHereTo={"update"} method={'PUT'}/>} />
          <Route path="/admin/products/add" element={<ProductForm isHereTo={"create"} method={'POST'}/>} />
          <Route path="/admin/questions" element={<AdminQuestions />} />
          <Route path="/admin/questions/edit/:questionId" element={<QuestionForm />} />
          <Route path="/purchases" element={<Purchases />} />
        </Routes>
      </Router>
  )
}

export default App