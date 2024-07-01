import React, { useState,useEffect } from 'react'
import './css/navbar.css';
import { search_icon } from '../assets';
import { useNavigate, Link } from 'react-router-dom';

import { login, login_hover, logged_in, logged_in_hover, admin, admin_hover } from '../assets';

const Navbar = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();
  const [ detailedSearch,setDetailedSearch ] = useState(false);
  const [loggedIn, setLoggedIn] = useState(false);
  const [loginHover, setLoginHover] = useState(false);

  const handleSearch = (event) => {
    event.preventDefault();
    if (searchTerm.length >= 3) {
      navigate(`/search?term=${encodeURIComponent(searchTerm)}&detailedSearch=${detailedSearch}`);
    } else {
      alert('Lütfen aramak için 3 harf veya daha uzun bir şeyler yazın.');
    }
  };
  useEffect(()=>{
    if(localStorage.getItem("user") !== null)
    {
      setLoggedIn(true);
    }
    else
    {
      setLoggedIn(false); 
    }
  },[localStorage.getItem("user")])

  return (
    <div className='navbar_parent'>
      <div className="navbar">
        <Link className="login_container" to={loggedIn ? JSON.parse(localStorage.getItem("user")) && JSON.parse(localStorage.getItem("user")).admin ? "/admin" : "/user" : "/login"}>
          {
            loggedIn?
              JSON.parse(localStorage.getItem("user")) && JSON.parse(localStorage.getItem("user")).admin ?
                <img src={loginHover ? admin : admin_hover} onMouseEnter={() => setLoginHover(true)} onMouseLeave={() => setLoginHover(false)} alt="" />
              :
                <img src={loginHover ? logged_in : logged_in_hover} onMouseEnter={() => setLoginHover(true)} onMouseLeave={() => setLoginHover(false)} alt="" />
            :
              <img src={loginHover ? login : login_hover} onMouseEnter={() => setLoginHover(true)} onMouseLeave={() => setLoginHover(false)} alt="" />
          }
        </Link>
        <Link to="/categories/fotograf-makineleri">
          Kategoriler
        </Link>
        <Link className="name" to="/">
          <p className='wise'>
            Wise
          </p>
          <p className='soft'>
            Soft
          </p>
        </Link>
        <Link to="/brands">
          Markalar
        </Link>
          <form onSubmit={handleSearch} className='search-bar'>
            <div className={`detailed-search ${detailedSearch ? "active":""}`} onClick={() => {setDetailedSearch(!detailedSearch)}}>
              Detaylı Arama {detailedSearch?"Açık":"Kapalı"}
            </div>
            <input
              type="text"
              placeholder='Lütfen 3 harf veya daha uzun bir şeyler yazın.'
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <button type="submit">
              <img src={search_icon} alt="Ara" />
            </button>
          </form>
      </div>
    </div>
  )
}

export default Navbar