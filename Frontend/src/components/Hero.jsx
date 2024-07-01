import { useState,useEffect } from "react"

import { useNavigate } from "react-router-dom"

import HeroProduct from "./HeroProduct"

import "./css/hero.css"

const Hero = () => {
    const [bannersLoading, setBannersLoading] = useState(true)
    const [banners,setBanners] = useState([])

    const [bestSellersLoading, setBestSellersLoading] = useState(true)
    const [bestSellers,setBestSellers] = useState([])
    
    const [bestSellersPage,setBestSellersPage] = useState(0);

    const [error, setError] = useState(null);

    const navigate = useNavigate();

    const handleBestSellersPage = (nextPage) =>
    {
      if (!bestSellersLoading) {
        if (nextPage && !bestSellers.last) {
          setBestSellersPage(bestSellersPage+1);
        }
        else if (!nextPage && !bestSellers.first)
        {
          setBestSellersPage(bestSellersPage-1);
        }
      }
    } 

    const fetchHeroProducts = async (page) => {
      try {
        const response = await fetch(`http://localhost:8080/products/hero_products?page=${page}`);
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data = await response.json();
        setBestSellers(data);
        setBestSellersLoading(false);
      } catch (error) {
        setError(error.message);
      }
    };

    useEffect(() => {
      fetchHeroProducts(0);

      const fetchBanners = async()=>{
        try {
          const response = await fetch("http://localhost:8080/banners/home");
            if (!response.ok) {
              throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setBanners(data);
            setBannersLoading(false);
          } catch (error) {
            setError(error.message);
          }
      }
      fetchBanners();
    }, []);

    useEffect(()=>{
      setBestSellersLoading(true);
      fetchHeroProducts(bestSellersPage);
    },[bestSellersPage])


  // Auto Slider Fonksiyonları

  const [currentSlide, setCurrentSlide] = useState(0);

  useEffect(() => { // 5 saniyede bir slayt değiştirmek için timer ayarla, eğer şuanki slayt son slayt ise başa dön değil ise bir sağa git 
    const timer = setInterval(() => {
      setCurrentSlide(prevSlide =>
        prevSlide === banners.length - 1 ? 0 : prevSlide + 1
      );
    }, 5000); // Zamanlayıcıyının milisaniye ile ayarlanması

    return () => clearInterval(timer); // Unmount edildiğinde zamanlayıcıyı temizle
  }, [banners.length]);

  const handleNavigate = (navigateDir) =>
  {
    if (navigateDir) {
      navigate("/"+navigateDir);
    }
  }
  
    if (error) {
      return <div>Error: {error}</div>;
    }

    return (
      <div className="hero_parent">
        {
          bannersLoading?
            <div>
              Banner Yükleniyor...
            </div>
          :
          <section className="banners-container">
            {banners.length }
              <div className="slide-wrapper">
                <div className="slider">
                  {banners.map((banner,index)=>(
                      <img src={`http://localhost:8080/images/${banner.image.imageId}`} alt="" id={`slide-${index}`}
                          onClick={() => {window.open(`${banner.bannerRouteLink}`, "_blank", "noopener")}}
                          style={{ transform: `translateX(-${currentSlide * 100}%)` }}
                      />
                  ))}
                </div>
                <div className="slider-nav">
                  {banners.length>1 && (
                    banners.map((banner,index)=>(
                      <div href={`#slide-${index}`} onClick={()=>setCurrentSlide(index)} className={currentSlide===index &&("active")}></div>
                  ))
                  )}
                </div>
              </div>
          </section>
        }
        <div className="hero-products">
          <div className="hero-product-title">
            <p>En Çok Satanlar</p>
          </div>
          {
            !bestSellersLoading?
            <div style={{transform:"translateY(-5px)"}}>
              {
                bestSellers.content.map(
                  (product) =>(
                    <HeroProduct product={product}/>
                  )
                )
              }
              {
                bestSellers.totalPages > 1 &&
                (
                  <div className="hero-page-changer-container page-changer-with-arrows">
                    <div className={`page-button-arrow ${!bestSellers.first ? "active":""}`} onClick={() => handleBestSellersPage(false)}>
                        {"<"}
                    </div>
                    <div className={`page-button-arrow ${!bestSellers.last ? "active":""}`} onClick={() => handleBestSellersPage(true)}>
                        {">"}
                    </div>
                  </div>
                )
              }
              <div className="show-all-products" onClick={() => handleNavigate("products")}>
                Tüm Ürünleri Göster
              </div>
            </div>
            :
            <div>
              En Çok Satan Ürünler Yükleniyor...
            </div>
          }
        </div>
      </div>
    )
}

export default Hero