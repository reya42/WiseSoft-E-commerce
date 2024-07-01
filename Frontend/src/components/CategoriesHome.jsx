import React from 'react'
import { useState,useEffect } from 'react'
import CategoryHomeProducts from './CategoryHomeProducts';

import { upsidedown_arrow,upsidedown_arrow_active } from '../assets';

import './css/categoryhome.css'

const CategoriesHome = () => {
  const [categories, setCategories] = useState([]);
  const [categoryProducts, setCategoryProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isThereAnyThingToRender,setAnythingToRender] = useState(false);
  const fetchCategories = async () => {
    try {
      const response = await fetch('http://localhost:8080/home/categories');
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      setCategories(data);
      return data;
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const fetchProductsForCategory = async (categoryLink,page) => {
    try {
      if (!page) {
        page = 0;
      }
      const response = await fetch(`http://localhost:8080/category/${categoryLink}/home?page=${page}`);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error fetching products for category:', error);
    }
  };

  const fetchAllProductsForAllCategories = async () => {
    const categoriesData = await fetchCategories();
    if (categoriesData) {
      let updateCategoriesPages = [];
      for (let index = 0; index < categoriesData.length; index++) {
        updateCategoriesPages.push(
          ...updateCategoriesPages, 0
        )
      }
      setCategoriesPages(updateCategoriesPages);
      const productsPromises = categoriesData.map(async (category) => {
        const products = await fetchProductsForCategory(category.categoryLink);
        if (products){
          if (products.content.length>0) {
            setAnythingToRender(true);
          }
        };
        return products;
      });
      Promise.all(productsPromises).then(productsArrays => {
        setCategoryProducts(productsArrays);
        setLoading(false);
      });
    }
  };
  useEffect(() => {
    fetchAllProductsForAllCategories();
  }, []);
  
    const [categoriesPages,setCategoriesPages] = useState([]);

    const handlePageChange = async (categoryIndex, add) => {
      const thisCategory = categories[categoryIndex];
      let newPage = 0;
      let pageChanged = false;
      if (add && !categoryProducts[categoryIndex].last) { // eğer eklenecekse last olmaması lazım.
        newPage = categoriesPages[categoryIndex] + 1;
        pageChanged = true;
      }
      else if (!add && !categoryProducts[categoryIndex].first) // eğer eklenmeyecekse first olmaması lazım.
      {
        newPage = categoriesPages[categoryIndex] - 1;
        pageChanged = true;
      }
      if (pageChanged) {    // eğer yeni sayfa şuanki sayfadan farklıysa güncelle
        let products = await fetchProductsForCategory(thisCategory.categoryLink,newPage);
        if (products.content.length>0) {
            let updatedProducts = [...categoryProducts];
            let updatedPages = [...categoriesPages];

            updatedProducts[categoryIndex] = products;
            updatedPages[categoryIndex] = newPage;

            setCategoryProducts(updatedProducts);
            setCategoriesPages(updatedPages);
        }
      }
    }

    return (
        !loading && isThereAnyThingToRender && (
            categories.map((category,categoryIndex) =>(
                <div className={`category-container${categoryIndex%2 === 1 ? " theme":" non-theme"}`}>
                    <div className="category-home-name">
                        {category.categoryName}
                    </div>
                    <CategoryHomeProducts products={categoryProducts[categoryIndex]} theme={true}/>
                    
                    {
                      categoryProducts[categoryIndex].totalPages > 1 && 
                        (
                          <div className='categories-home-page-changer'>
                            <div className="page-changer-with-arrows">
                              <div className="page-button-arrow-left" onClick={() => handlePageChange(categoryIndex, false)}>
                                {
                                  categoryProducts[categoryIndex].first ? 
                                    <img src={upsidedown_arrow} alt="" />
                                  :
                                    <img src={upsidedown_arrow_active} alt="" />
                                }
                              </div>
                              <div className="page-button-arrow-right" onClick={() => handlePageChange(categoryIndex, true)}>
                                {
                                  categoryProducts[categoryIndex].last ? 
                                    <img src={upsidedown_arrow} alt="" />
                                  :
                                    <img src={upsidedown_arrow_active} alt="" />
                                }
                              </div>
                            </div>
                          </div>
                        )
                    }
                </div>
            ))
        )
    );
}

export default CategoriesHome